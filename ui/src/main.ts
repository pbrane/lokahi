import { createApp } from 'vue'
import App from './App.vue'
import { createPinia } from 'pinia'
import VueKeycloak from '@dsb-norge/vue-keycloak-js'
import Keycloak from 'keycloak-js'
import keycloakConfig from '../keycloak.config'
import useKeycloak from './composables/useKeycloak'
import '@featherds/styles'
import '@featherds/styles/themes/open-light.css'
import dateFormatDirective from './directives/v-date'
import featherInputFocusDirective from './directives/v-focus'
import tabIndexDirective from './directives/v-tabindex'
import { getGqlClient } from './services/gqlService'

const { setKeycloak } = useKeycloak()

const app = createApp(App)
  .use(createPinia())
  .use(VueKeycloak, {
    init: {
      onLoad: 'login-required'
    },
    config: {
      realm: keycloakConfig.realm,
      url: keycloakConfig.url,
      clientId: keycloakConfig.clientId
    },
    onReady: (kc: Keycloak) => {
      setKeycloak(kc)
      const gqlClient = getGqlClient(kc)
      app.use(gqlClient)

      // FIXME: This is a workaround for these issues:
      //  https://github.com/keycloak/keycloak/issues/14742
      //  https://opennms.atlassian.net/browse/HS-1654
      //  VueJS's `createRouter` function interferes with the routing Keycloak does as part of its auth process.
      //  Keycloak needs to be fully initialized before the router is imported.
      //  Other components should prefer `const router = useRouter()`, importing `@/router` can cause this to regress.
      import('./router')
        .then((router) => {
          app.use(router.default)
          app.mount('#app')
        })
    }
  })
  .directive('date', dateFormatDirective)
  .directive('focus', featherInputFocusDirective)
  .directive('tabindex', tabIndexDirective)
