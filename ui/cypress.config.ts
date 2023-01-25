import { defineConfig } from 'cypress'
import cypressViteConfig from './cypress.vite.config'

export default defineConfig({
  component: {
    specPattern: 'cypress/component/**/*.cy.*.{js,jsx,ts,tsx}',
    devServer: {
      framework: 'vue',
      bundler: 'vite',
      viteConfig: cypressViteConfig
    }
  },

  e2e: {
    baseUrl: 'https://onmshs',
    specPattern: 'cypress/e2e/**/*.cy.*.{js,jsx,ts,tsx}',
    setupNodeEvents(on, config) {
      // implement node event listeners here
    }
  }
})
