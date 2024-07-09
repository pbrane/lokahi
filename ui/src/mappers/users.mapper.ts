import { CreateEditMode } from '@/types'
import { User as ServerUser, UserRepresentationInput } from '@/types/graphql'
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

export const mapUserToServer = (user: ClientUser, mode: CreateEditMode): UserRepresentationInput => {
  let userInput = {
    username: user.username,
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    origin: '',
    self: '',
    requiredActions: []
  } as UserRepresentationInput

  if (mode === CreateEditMode.Create) {
    userInput = {
      ...userInput,
      enabled: true,
      createdTimestamp: new Date().getTime(),
      credentials: [{
        createdDate: new Date().getTime(),
        type: 'password',
        temporary: false,
        userLabel: '',
        value: user.password
      }]
    }
  }

  if (mode === CreateEditMode.Edit) {
    userInput = {
      ...userInput,
      id: user.id,
      enabled: user.enabled,
      createdTimestamp: user.createdTimestamp,
      credentials: []
    }
  }

  return userInput
}
