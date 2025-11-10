const path = require("path");
const webpack = require("webpack");
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
  entry: "./src/index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "bundle.js",
    clean: true,
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: ["@babel/preset-env", "@babel/preset-react"],
          },
        },
      },
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: "./public/index.html",
    }),
    new webpack.DefinePlugin({
      "process.env": JSON.stringify({
        REACT_APP_API_URL: process.env.REACT_APP_API_URL || "http://localhost:3000",
        REACT_APP_SOCKET_URL: process.env.REACT_APP_SOCKET_URL || "http://localhost:3000",
      }),
    }),
  ],
  devServer: {
    port: 3001,
    hot: true,
    historyApiFallback: true,
  },
  resolve: {
    extensions: [".js", ".jsx"],
  },
};

