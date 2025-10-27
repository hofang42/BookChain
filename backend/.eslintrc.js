module.exports = {
  env: {
    node: true,
    es2021: true,
  },
  extends: ["eslint:recommended"],
  parserOptions: {
    ecmaVersion: 2021,
    sourceType: "script",
  },
  rules: {
    // allow console in backend, prefer warnings for unused vars
    "no-console": "off",
    "no-unused-vars": ["warn", { args: "none", ignoreRestSiblings: true }],
    "prefer-const": "warn",
  },
};
