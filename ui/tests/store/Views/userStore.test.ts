import { createTestingPinia } from '@pinia/testing'
import { setActiveClient, useClient } from 'villus'
import { useUserStore } from '@/store/Views/userStore'
import { CreateEditMode } from '@/types'
import { useUsersMutations } from '@/store/Mutations/usersMutations'
import { User } from '@/types/users'

describe('UserStore', () => {
  let userStore: ReturnType<typeof useUserStore>
  const UserDummyData: User = {
    id: 'test-id',
    email: 'test@example.com',
    username: 'testuser',
    firstName: 'test-first',
    lastName: 'test-lastName',
    password: 'sTR@ng123',
    roles: [],
    enabled: false,
    createdTimestamp: new Date('2012-12-12T00:00:00Z').getTime()
  }
  const defaultUser = {
    id: (new Date()).getTime().toString(),
    username: '',
    firstName: '',
    lastName: '',
    password: '',
    email: '',
    roles: [],
    enabled: false,
    createdTimestamp: new Date().getTime()
  }
  beforeAll(() => {
    createTestingPinia({ stubActions: false })
    setActiveClient(useClient({ url: 'http://test/graphql' })) // Create and set a client
  })

  beforeEach(() => {
    userStore = useUserStore()
  })


  it('should initialize with default state', () => {
    expect(userStore.usersList).toEqual([])
    expect(userStore.selectedUser).toBeUndefined()
    expect(userStore.validationErrors).toEqual({})
    expect(userStore.userEditMode).toBe(CreateEditMode.None)
    expect(userStore.isModalVisible).toBe(false)
  })

  it('should update userEditMode and selectedUser on updateUser', () => {
    const user = { ...defaultUser, id: 'user-1' }

    userStore.updateUser(user)

    expect(userStore.userEditMode).toBe(CreateEditMode.Edit)
    expect(userStore.selectedUser).toEqual(user)
  })

  it('should create a new user on createUser', () => {
    userStore.createUser()
    const currentTime = new Date().getTime()
    if (userStore.selectedUser) {
      expect(userStore.selectedUser.username).toBe('')
      expect(userStore.selectedUser.firstName).toBe('')
      expect(userStore.selectedUser.lastName).toBe('')
      expect(userStore.selectedUser.password).toBe('')
      expect(userStore.selectedUser.email).toBe('')
      expect(userStore.selectedUser.roles).toEqual([])
      expect(userStore.selectedUser.enabled).toBe(false)
      expect(Number(userStore.selectedUser.id)).toBeGreaterThanOrEqual(Number(defaultUser.id))
      expect(Number(userStore.selectedUser.id)).toBeLessThanOrEqual(currentTime)
      expect(Number(userStore.selectedUser.createdTimestamp)).toBeGreaterThanOrEqual(Number(defaultUser.createdTimestamp))
      expect(Number(userStore.selectedUser.createdTimestamp)).toBeLessThanOrEqual(currentTime)
    }
  })
  it('should validate user correctly', () => {
    const user = {...defaultUser}
    expect(userStore.validateUser(user)).toBe(false)
    expect(userStore.validationErrors).toEqual({
      username: 'Please enter a valid username',
      firstName: 'Please enter first name',
      lastName: 'Please enter last name',
      email: 'Please enter a valid email',
      password: 'Please enter a valid password'
    })
  })

  it('should check for duplicate username or email', () => {
    userStore.usersList = [UserDummyData]
    userStore.selectedUser = { ...defaultUser, email: 'test@example.com', username: 'testuser' }

    expect(userStore.checkForDuplicateUsernameOrEmail()).toBe(true)
  })

  it('should save a new user', async () => {
    userStore.selectedUser = UserDummyData
    await userStore.saveUser()

    expect(userStore.validationErrors).toEqual({})
    expect(userStore.isModalVisible).toBe(false)
  })

  it('should update user data', async () => {
    const { updateUser } = useUsersMutations()
    userStore.selectedUser = { ...UserDummyData, email: 'updated@example.com', username: 'updateduser' }
    userStore.updateUser(userStore.selectedUser)

    await userStore.updateUserData()

    expect(userStore.validationErrors).toEqual({})
    expect(userStore.isModalVisible).toBe(false)
    expect(updateUser).toHaveBeenCalled()
  })

  it('should handle modal visibility', () => {
    userStore.openModalHandler()
    expect(userStore.isModalVisible).toBe(true)

    userStore.closeModalHandler()
    expect(userStore.isModalVisible).toBe(false)
  })

})
