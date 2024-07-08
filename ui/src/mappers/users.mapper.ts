import { User as ServerUser } from '@/types/graphql'
import { User as ClientUser } from '@/types/users'

export const mapUserFromServer = (user: ServerUser): ClientUser => {
  return {
    id: user.id,
    username: user.username || '',
    firstName: user.firstName || '',
    lastName: user.lastName || '',
    password: user.password || '',
    enabled: user.enabled || false,
    email: user.email || '',
    roles: user.roles || [],
    createdTimestamp: user.createdTimestamp || 0
  } as ClientUser
}
