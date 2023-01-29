import { defineConfig } from 'cypress'
// import { env } from 'process'
// import cypressViteConfig from './cypress.vite.config'
import viteConfig from './vite.config'
// import { loadEnv } from 'vite'
// import loadenv from 'loadenv'

// const env = loadEnv('development', process.cwd())
// const env = loadEnv('', process.cwd())
// const env = loadenv.restore()
// console.log('env', env)

// console.log('>>> ', Cypress.env['baseUrl'])

// console.log('process.env', process.env)

export default defineConfig({
  /* env: {
    base_url: 'http://defineConfig.env'
  }, */
  component: {
    specPattern: 'cypress/component/**/*.cy.*.{js,jsx,ts,tsx}',
    devServer: {
      framework: 'vue',
      bundler: 'vite',
      viteConfig
      // viteConfig: cypressViteConfig
    }
  },

  e2e: {
    // baseUrl: env.baseUrl,
    // baseUrl: Cypress.env['baseUrl'],
    baseUrl: 'https://onmshs',
    // baseUrl: 'http://localhost:8123', // local
    // baseUrl: env.VITE_CYPRESS_BASE_URL, // local
    // "base_url": "http://localhost:8123"
    // baseUrl: Cypress.env('base_url'),
    specPattern: 'cypress/e2e/**/*.cy.*.{js,jsx,ts,tsx}',
    setupNodeEvents(on, config) {
      // console.log('***** on', on)
      // implement node event listeners here
      // console.log('process.env', process.env)
      // console.log('config', config)
      // return {
      //   ...config,
      //   baseUrl: 'https://onmshs'
      // }
    }
  }
})
