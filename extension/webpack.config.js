/* eslint-disable no-useless-computed-key */
const path = require('path');
const packageJson = require('./package.json');
const FileManagerPlugin = require('filemanager-webpack-plugin');
const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer');
const Visualizer = require('webpack-visualizer-plugin2');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const { DuplicatesPlugin } = require('inspectpack/plugin');

const isProdEnv = process.env.NODE_ENV === 'production';

const config = {
  target: 'web',
  mode: process.env.NODE_ENV || 'development',
  entry: {
    index: path.resolve(__dirname, 'src', 'index.ts'),
    content: path.resolve(__dirname, 'src', 'content.ts'),
  },
  optimization: {
    minimize: false
  },
  module: {
    rules: [
      {
        test: /\.(ts|js)x?$/,
        exclude: /node_modules/,
        loader: 'babel-loader',
      }
    ],
  },
  resolve: {
    extensions: ['.ts', '.tsx', '.js', '.json'],
  },
  // prettier-ignore
  output: {
    path: path.resolve(__dirname, 'dist'),
  },
  plugins: [
    new CleanWebpackPlugin(),
    new FileManagerPlugin({
      events: {
        onEnd: {
          copy: [
            {
              source: path.join(__dirname, 'src', 'manifest.json'),
              destination: path.join(__dirname, 'dist', 'manifest.json'),
            },
            {
              source: path.join(__dirname, 'icons', '*'),
              destination: path.join(__dirname, 'dist', 'icons'),
            },
            ...(!isProdEnv
              ? [
                  {
                    source: path.join(__dirname, 'public', 'index.html'),
                    destination: path.join(__dirname, 'dist', 'index.html'),
                  },
                ]
              : []),
          ],
          ...(isProdEnv && {
            delete: [
              path.join(__dirname, '*.zip'),
            ],
            archive: [
              {
                source: path.join(__dirname, 'dist'),
                destination: path.join(
                  __dirname,
                  `firefox-extension-${packageJson.version}.zip`
                ),
              },
            ],
          }),
        },
      },
    }),
  ],
};

if (process.env.NODE_ENV === 'developmemt') {
  config.devtool = 'eval-source-map';
}

if (process.env.NODE_ENV === 'production') {
  config.plugins = [
    ...config.plugins,
    new DuplicatesPlugin({
      emitErrors: false,
      emitHandler: undefined,
      ignoredPackages: undefined,
      verbose: false,
    }),
  ];
}

if (process.env.ANALYZE) {
  config.plugins = [...config.plugins, new BundleAnalyzerPlugin()];
}

if (process.env.VISUALIZE) {
  config.plugins = [...config.plugins, new Visualizer()];
}

module.exports = config;
