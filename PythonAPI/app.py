from flask import Flask, request
from flask_restful import Api, Resource, reqparse
from pathlib import Path
from tensorflow import keras
import numpy as np
import tensorflow as tf
import json

app = Flask(__name__)
api = Api(app)

num_games = 100
output_size = 64**2
learning_rate = 0.001
model_name = "chessNNConv3"
params_conv2d = {
    "padding": "SAME",
    "activation": keras.activations.elu,
    "data_format": "channels_first"
}


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

    global graph
    graph = tf.get_default_graph()

    if Path("models/" + model_name + '.h5').is_file():
        print("loading weights")
        model.load_weights("models/" + model_name + '.h5')

    model.compile(optimizer=keras.optimizers.Adam(lr=learning_rate),
                  loss='categorical_crossentropy',
                  metrics=['categorical_accuracy', 'top_k_categorical_accuracy'])

    return model


model = init_model()


@app.route('/<position>')
def get(position):
    with graph.as_default():
        prediction = model.predict(np.array([np.array([format_input(position), ]), ]))
        return json.dumps(prediction[0].tolist())


def format_input(board):
    board_arr = []
    for x in range(8):
        board_arr.append([])
        for y in range(8):
            board_arr[x].append(ord(board[x * 8 + y]))
    return board_arr


if __name__ == '__main__':
    app.run()
