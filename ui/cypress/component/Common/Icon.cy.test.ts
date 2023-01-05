import Icon from '@/components/Common/Icon.vue'
import BubbleChart from '@material-design-icons/svg/outlined/bubble_chart.svg'

describe('Icon', () => {
  it('Should display an icon', () => {
    cy.mount(Icon, {
      props: {
        icon: {
          image: BubbleChart,
          title: 'Bubble Chart'
        }
      }
    })

    cy.get('svg').should('be.visible') // ok
    cy.get('svg.feather-icon').should('be.visible') // ok
    // cy.get('svg.feather-icon1').should('not.be.visible') // Expected to find element: svg.feather-icon1, but never found it.
  })
})