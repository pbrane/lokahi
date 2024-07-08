/**
 * Similar to graphql.ts/User.
 */
export interface User {
  id: string
  firstName: string
  lastName: string
  password: string
  email: string
  roles: string[]
  enabled: boolean
  username: string
  createdTimestamp: number
}
