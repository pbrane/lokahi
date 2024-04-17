## Build instructions

This project requires Node v20+.

You will also need [yarn](https://yarnpkg.com/getting-started/install) 1.22.22.

Built using Vue, Vite, Vitest, Typescript.

To install packages and run dev server:

```
yarn install
yarn dev
```

Build for prod:

```
yarn build
```

Run unit tests:

```
yarn test
```

Run linter:

```
yarn lint
```

Run linter and fix issues:

```
yarn lint:fix
```

## State management with pinia

This project uses [Pinia](https://pinia.vuejs.org/) for state management.
Each store module has separate files for Views, Queries and Mutations.
Current convention is to only call actions from components (no mutations).

## Vue-router

Project routes make use of [vue-router](https://next.router.vuejs.org/guide/)

## ESLint

Formatting should use the `.eslintrc`. For VSCode, install the ESLint extension, go to the IDE Settings and set this formatter to take precedence.

### Use `<script setup>`

[`<script setup>`](https://github.com/vuejs/rfcs/pull/227). To get proper IDE support for the syntax, use the [Vue 3 Support](https://marketplace.visualstudio.com/items?itemName=Wscats.vue) extension.

## Templates (development purposes only)

Hidden route serving templates: /templates
