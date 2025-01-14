import { createId } from '@/components/utils'
import { CreateEditMode } from '@/types'
import { User } from '@/types/users'
import { defineStore } from 'pinia'
import { useUserQueries } from '../Queries/userQueries'
import { mapUserFromServer, mapUserToServer } from '@/mappers/users.mapper'
import { UserRepresentationInput } from '@/types/graphql'
import { useUsersMutations } from '../Mutations/usersMutations'
import { cloneDeep } from 'lodash'
import useSnackbar from '@/composables/useSnackbar'

const { showSnackbar } = useSnackbar()

type TState = {
  usersList?: User[],
  selectedUser?: User,
  validationErrors: any,
  userEditMode: CreateEditMode,
  isModalVisible: boolean
}

const defaultUser: User = {
  id: `${createId()}`,
  username: '',
  firstName: '',
  lastName: '',
  password: '',
  email: '',
  roles: [],
  enabled: false,
  createdTimestamp: new Date().getTime()
}

const EMAIL_REGEX = new RegExp(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)
const PASSWORD_REGEX = new RegExp(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)

export const useUserStore = defineStore('userStore', {
  state: (): TState => ({
    usersList: [],
    selectedUser: undefined,
    validationErrors: {},
    userEditMode: CreateEditMode.None,
    isModalVisible: false
  }),
  actions: {
    async getUsersList() {
      const queries = useUserQueries()
      await queries.loadAllUsers()
      this.usersList = queries.allUsersList.map(mapUserFromServer)
    },
    updateUser(user: User) {
      this.userEditMode = CreateEditMode.Edit
      this.selectedUser = cloneDeep(user)
    },
    createUser() {
      this.userEditMode = CreateEditMode.Create
      this.selectedUser = {
        ...cloneDeep(defaultUser),
        id: `${createId()}`,
        createdTimestamp: new Date().getTime()
      }
    },
    checkForDuplicateUsernameOrEmail(): boolean {
      let userList: User[] | undefined
      if (this.userEditMode === CreateEditMode.Edit) {
        userList = this.usersList?.filter(user => user.id !== this.selectedUser?.id)
      } else {
        userList = this.usersList
      }

      const duplicateUser = userList?.find((user: User) =>
        user.email.toLowerCase() === this.selectedUser?.email.toLowerCase() ||
        user.username.toLowerCase() === this.selectedUser?.username.toLowerCase()
      )

      if (duplicateUser) {
        if (duplicateUser.email.toLowerCase() === this.selectedUser?.email.toLowerCase()) {
          showSnackbar({ msg: 'User exists with the same email address.', error: true })
        } else if (duplicateUser.username.toLowerCase() === this.selectedUser?.username.toLowerCase()) {
          showSnackbar({ msg: 'User exists with the same username.', error: true })
        }
        return true
      }
      return false
    },
    validateUser(user: User) {
      const firstName = (user.firstName.trim() || '').toLowerCase()
      const lastName = (user.lastName.trim() || '').toLowerCase()
      const email = (user.email.trim() || '').toLowerCase()
      const password = (user.password.trim() || '')
      const username = (user.username.trim() || '').toLowerCase()
      let isValid = true
      if (!username) {
        this.validationErrors.username = 'Please enter a valid username'
        isValid = false
      }
      if (!firstName) {
        this.validationErrors.firstName = 'Please enter first name'
        isValid = false
      }
      if (!lastName) {
        this.validationErrors.lastName = 'Please enter last name'
        isValid = false
      }
      if (!EMAIL_REGEX.test(email)) {
        this.validationErrors.email = 'Please enter a valid email'
        isValid = false
      }
      if (!PASSWORD_REGEX.test(password) && this.userEditMode === CreateEditMode.Create) {
        this.validationErrors.password = 'Please enter a valid password'
        isValid = false
      }
      return isValid
    },
    async saveUser() {
      this.validationErrors = {}
      if (!this.selectedUser || !this.validateUser(this.selectedUser) || this.checkForDuplicateUsernameOrEmail()) {
        return
      }

      const {createNewUser, errorWhileCreatingUser} = useUsersMutations()
      const userInput: UserRepresentationInput = mapUserToServer(this.selectedUser, this.userEditMode)

      await createNewUser({user: userInput})

      if (!errorWhileCreatingUser.value) {
        this.closeModalHandler()
        await this.getUsersList()
      } else {
        showSnackbar({
          msg: 'Unexpected error occurred while creating User.',
          error: true
        })
      }
    },
    async updateUserData() {
      this.validationErrors = {}
      if (!this.selectedUser || !this.validateUser(this.selectedUser) || this.checkForDuplicateUsernameOrEmail()) {
        return
      }

      const {updateUser, errorWhileUpdatingUser} = useUsersMutations()
      const userInput: UserRepresentationInput = mapUserToServer(this.selectedUser, this.userEditMode)

      await updateUser({user: userInput})

      if (!errorWhileUpdatingUser.value) {
        this.closeModalHandler()
        await this.getUsersList()
      } else {
        showSnackbar({
          msg: 'Unexpected error occurred while updating User.',
          error: true
        })
      }

    },
    clearSelectedUser() {
      this.selectedUser = undefined
    },
    closeModalHandler() {
      this.userEditMode = CreateEditMode.None
      this.validationErrors = {}
      this.clearSelectedUser()
      this.isModalVisible = false
    },
    openModalHandler() {
      this.isModalVisible = true
    }
  }
})
