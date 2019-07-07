from datetime import datetime
from pathlib import Path

import chess
import chess.engine
import numpy as np
from tensorflow import keras

num_games = 5000
output_size = 64**2
learning_rate = 0.001
model_name = "chessNNConv2"
params_conv2d = {
    "padding": "SAME",
    "activation": keras.activations.elu,
    "data_format": "channels_first"
}


def square_to_bit_index(square):
    return (ord(square[0]) - 97) + (ord(square[1]) - 49) * 8


def algebraic_to_move_index(move):
    start = square_to_bit_index(move[:2])
    end = square_to_bit_index(move[2:4])
    return int(start) * 64 + int(end)


def to_X(board):
    board = str(board).replace(" ", "").replace("\n", "").replace("\r", "")
    board_arr = []
    for x in range(8):
        board_arr.append([])
        for y in range(8):
            board_arr[x].append(ord(board[x * 8 + y]))
    return board_arr


def to_Y(data):
    label = np.zeros(output_size)
    data = [(x[0], int(x[1].replace("#", ""))) for x in data]
    min_score = min([x[1] for x in data])
    max_score = max([x[1] for x in data])

    for x in data:
        label[algebraic_to_move_index(str(x[0]))] = normalize(x[1], min_score, max_score)

    return label.tolist()


def split_data(data):
    return [x[0] for x in data], [x[1] for x in data]


def normalize(x, min, max):
    if max == min:
        return 0
    else:
        return (x - min) / (max - min)


def learning_rate_scheduler(epoch):
    if epoch < 600:
        return learning_rate
    elif 600 <= epoch < 900:
        return learning_rate / 2
    else:
        return learning_rate / 4


def init_model():
    nn_input = keras.Input((1, 8, 8))

    conv1_1 = keras.layers.Conv2D(filters=8, kernel_size=3, strides=1, **params_conv2d)(nn_input)
    conv1_1 = keras.layers.SpatialDropout2D(.3)(conv1_1)
    conv1_2 = keras.layers.Conv2D(filters=8, kernel_size=3, strides=2, **params_conv2d)(conv1_1)
    conv1_2 = keras.layers.SpatialDropout2D(.3)(conv1_2)

    conv2_1 = keras.layers.Conv2D(filters=16, kernel_size=2, strides=1, **params_conv2d)(conv1_2)
    conv2_1 = keras.layers.SpatialDropout2D(.3)(conv2_1)
    conv2_2 = keras.layers.Conv2D(filters=16, kernel_size=2, strides=1, **params_conv2d)(conv2_1)
    conv2_2 = keras.layers.SpatialDropout2D(.3)(conv2_2)
    conv2_3 = keras.layers.Conv2D(filters=16, kernel_size=3, strides=1, **params_conv2d)(conv2_2)
    conv2_3 = keras.layers.SpatialDropout2D(.3)(conv2_3)
    conv2_4 = keras.layers.Conv2D(filters=16, kernel_size=3, strides=2, **params_conv2d)(conv2_3)
    conv2_4 = keras.layers.SpatialDropout2D(.3)(conv2_4)

    conv3_1 = keras.layers.Conv2D(filters=32, kernel_size=3, strides=1, **params_conv2d)(conv2_4)
    conv3_1 = keras.layers.SpatialDropout2D(.3)(conv3_1)
    conv3_2 = keras.layers.Conv2D(filters=32, kernel_size=3, strides=1, **params_conv2d)(conv3_1)
    conv3_2 = keras.layers.SpatialDropout2D(.3)(conv3_2)
    conv3_3 = keras.layers.Conv2D(filters=32, kernel_size=3, strides=1, **params_conv2d)(conv3_2)
    conv3_3 = keras.layers.SpatialDropout2D(.3)(conv3_3)
    conv3_4 = keras.layers.Conv2D(filters=32, kernel_size=3, strides=1, **params_conv2d)(conv3_3)
    conv3_4 = keras.layers.SpatialDropout2D(.3)(conv3_4)

    flat = keras.layers.Flatten()(conv3_4)
    dense_output = keras.layers.Dense(output_size)(flat)
    softmax = keras.layers.Softmax()(dense_output)

    model = keras.Model(inputs=nn_input, outputs=softmax)

    if Path("models/" + model_name + '.h5').is_file():
        print("loading weights")
        model.load_weights("models/" + model_name + '.h5')

    model.compile(optimizer=keras.optimizers.Adam(lr=learning_rate),
                  loss='categorical_crossentropy',
                  metrics=['categorical_accuracy', 'top_k_categorical_accuracy'])

    return model


engine = chess.engine.SimpleEngine.popen_uci("Stockfish10/Windows/stockfish_10_x64.exe")
model = init_model()
callbacks = [keras.callbacks.LearningRateScheduler(learning_rate_scheduler, verbose=0),
             keras.callbacks.ModelCheckpoint('checkpoints/' + model_name + datetime.now().strftime("D%d-H%H") + '.h5',
                                             monitor='val_categorical_accuracy',
                                             verbose=0,
                                             save_weights_only=False,
                                             mode='max',
                                             period=1000)]


def format_input(board):
    board_arr = []
    for x in range(8):
        board_arr.append([])
        for y in range(8):
            board_arr[x].append(ord(board[x * 8 + y]))
    return board_arr


def main():
    train_data = []
    for x in range(num_games):
        print("Starting game:" + str(x))
        board = chess.Board()
        while not board.is_game_over() and board.fullmove_number < 20:
            legal_moves = board.legal_moves
            data = []
            if board.turn == chess.BLACK:
                for move in legal_moves:
                    board.push(move)
                    # Estimate moves
                    info = engine.analyse(board, chess.engine.Limit(time=0.0100, depth=6))
                    board.pop()
                    data.append((move, str(info["score"])))
                train_data.append(([to_X(board)], to_Y(data)))

            # Make best move
            result = engine.play(board, chess.engine.Limit(time=0.100))
            board.push(result.move)
            # Make random move
            # board.push(random.choice(data)[0])
        if (x + 1) % 96 == 0:
            train_data_formatted = split_data(train_data)
            model.fit([train_data_formatted[0]], [train_data_formatted[1]], epochs=1050, batch_size=32, callbacks=callbacks, verbose=2)
            train_data = []
            model.save("checkpoints/" + model_name + datetime.now().strftime("D%d-H%H-M%M") + ".h5")
            model.save("models/" + model_name + ".h5")
    engine.quit()


if __name__ == '__main__':
    main()
