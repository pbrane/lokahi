import {
  DownloadMinionCertificateForWelcomeDocument,
} from '@/types/graphql'
import { MaybeLazyOrRef, useQuery } from 'villus'
import { QueryNotificationOptions, QueryOptions } from './queryService'
import { QueryService } from '.'

export const downloadCertificateBundle = (location: string) => {
  const queryOptions: QueryOptions<{ location: string }> = {
    query: DownloadMinionCertificateForWelcomeDocument,
    variables: { location }
  }

  const notificationOptions: QueryNotificationOptions = {
    notifyOnError: true,
    notifyOnSuccess: true
  }

  return QueryService.executeQuery(queryOptions, notificationOptions)
}
