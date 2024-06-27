import { createId } from '@/components/utils'
import { CreateEditMode } from '@/types'
import { User } from '@/types/users'
import { defineStore } from 'pinia'

type TState = {
  usersList?: User[],
  selectedUser?: User,
  validationErrors: any,
  userEditMode: CreateEditMode
}

const defaultUser: User = {
  id: createId(),
  firstName: '',
  lastName: '',
  password: '',
  email: '',
  role: '',
  enabled: false
}

const EMAIL_REGEX = new RegExp(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)
const PASSWORD_REGEX = new RegExp(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)

export const useUserStore = defineStore('userStore', {
  state: (): TState => ({
    usersList: [],
    selectedUser: undefined,
    validationErrors: {},
    userEditMode: CreateEditMode.None
  }),
  actions: {
    async getUsersList() {
      // Fetch User List Query
    },
    updateUser(user: User) {
      this.userEditMode = CreateEditMode.Edit
      this.selectedUser = user
      console.log(this.selectedUser)
    },
    createUser() {
      this.userEditMode = CreateEditMode.Create
      this.selectedUser = defaultUser
    },
    validateUser(user: User) {
      const firstName = (user.firstName.trim() || '').toLowerCase()
      const lastName = (user.lastName.trim() || '').toLowerCase()
      const email = (user.email.trim() || '').toLowerCase()
      const password = (user.password.trim() || '')
      let isValid = true
      if (!firstName) {
        this.validationErrors.firstName = 'Please enter first name'
        isValid = false
      } else if (!lastName) {
        this.validationErrors.lastName = 'Please enter last name'
        isValid = false
      } else if (!EMAIL_REGEX.test(email)) {
        this.validationErrors.email = 'Please enter a valid email'
        isValid = false
      } else if (!PASSWORD_REGEX.test(password)) {
        this.validationErrors.password = 'Please enter a valid password'
        isValid = false
      }
      return isValid
    },
    saveUser() {
      if (!this.selectedUser || !this.validateUser(this.selectedUser)) {
        return
      }

      // Save Logic Here

      // On Successfully saving user

      this.userEditMode = CreateEditMode.None

    },
    deleteUser(id: number) {
      if (id) {
        // Delete user Logic
      } else {
        throw new Error('Select a user to delete')
      }
    }
  }
})
