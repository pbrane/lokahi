import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import svgLoader from 'vite-svg-loader'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import featherResolver from './auto-feather-resolver'

// export default defineConfig(({ mode }) => {
export default defineConfig((args) => {
  console.log('args', args)
  // const env = loadEnv(mode, process.cwd())
  const mode = args?.mode || 'development'
  const env = loadEnv(mode, process.cwd())
  // console.log("new URL('./src/', import.meta.url)", new URL('./src/', import.meta.url))
  // console.log("new URL('./src/', import.meta.url).pathname", new URL('./src/', import.meta.url).pathname)
  // console.log('env', env)

  // console.log('>>>>>> import.meta', import.meta)
  // console.log('>>>>>> import.meta.env', import.meta.env)

  return {
    server: {
      port: parseInt(env.VITE_SERVER_PORT)
      // port: 9999
    },
    resolve: {
      alias: {
        '@/': new URL('./src/', import.meta.url).pathname,
        '~@featherds': '@featherds'
      },
      dedupe: ['vue']
    },
    plugins: [
      vue(),
      svgLoader(),
      AutoImport({
        imports: ['vue', 'vue-router', '@vueuse/core'],
        eslintrc: {
          enabled: true,
          filepath: './.eslintrc-auto-import.json'
        }
      }),
      Components({
        resolvers: [featherResolver]
      })
    ],
    test: {
      globals: true,
      environment: 'happy-dom',
      deps: {
        inline: true
      },
      coverage: {
        reporter: ['lcov', 'html']
      }
    }
  }
})
