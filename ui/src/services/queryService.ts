import useSnackbar from '@/composables/useSnackbar'
import { DocumentTypeDecoration, TypedDocumentNode } from '@graphql-typed-document-node/core'
import { DocumentNode } from 'graphql'
import { CachePolicy, MaybeRef, useQuery } from 'villus'

export interface QueryOptions<T> {
  query: MaybeRef<string | DocumentNode | TypedDocumentNode<any, any> | DocumentTypeDecoration<any, any>>
  variables?: T
}

export interface QueryNotificationOptions {
  notifyOnError: boolean
  notifyOnSuccess: boolean
  successMessage?: string
  errorMessage?: string
}

export const defaultNotificationOptions: QueryNotificationOptions = {
  notifyOnError: true,
  notifyOnSuccess: false,
  successMessage: '',
  errorMessage: ''
}

export const executeQuery = async <T>(
  queryOptions: QueryOptions<T>,
  notificationOptions: QueryNotificationOptions = defaultNotificationOptions,
  cachePolicy: CachePolicy | undefined = 'network-only'
) => {
  const { execute } = useQuery({
    query: queryOptions.query,
    cachePolicy,
    fetchOnMount: false,
    variables: queryOptions.variables
  })
  const { data, error } = await execute()
  const { showSnackbar } = useSnackbar()
  if (error && notificationOptions.notifyOnError) {
    showSnackbar({ msg: error.message || notificationOptions.errorMessage || '', error: true })
  } else if (!error && notificationOptions.notifyOnSuccess) {
    showSnackbar({ msg: notificationOptions.successMessage || '' })
  }
  return data;
}