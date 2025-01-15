module.exports = {
  env: {
    browser: true,
    es2021: true,
  },
  extends: ['plugin:react/recommended', 'standard-with-typescript'],
  overrides: [],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
    project: ['./tsconfig.json'],
  },
  plugins: ['react'],
  settings: {
    react: {
      version: 'detect',
    },
  },
  rules: {
    semi: 0,
    'comma-dangle': 0,
    'multiline-ternary': 0,
    '@typescript-eslint/semi': 0,
    '@typescript-eslint/comma-dangle': 0,
    '@typescript-eslint/member-delimiter-style': 0,
    '@typescript-eslint/no-non-null-assertion': 0,
    '@typescript-eslint/space-before-function-paren': 0,
    '@typescript-eslint/no-misused-promises': 0,
  },
};
