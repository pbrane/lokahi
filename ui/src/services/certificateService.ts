import {
  DownloadMinionCertificateForWelcomeDocument,
} from '@/types/graphql'
import { MaybeLazyOrRef, useQuery } from 'villus'
import { QueryNotificationOptions, QueryOptions } from './queryService'
import { QueryService } from '.'

export const downloadCertificateBundle = async (locationId: number) => {
  const queryOptions: QueryOptions<{ location: number }> = {
    query: DownloadMinionCertificateForWelcomeDocument,
    variables: { location: locationId }
  }

  const notificationOptions: QueryNotificationOptions = {
    notifyOnError: true,
    notifyOnSuccess: true
  }
  const minionCertificate = await QueryService.executeQuery(queryOptions, notificationOptions)
  return minionCertificate.getMinionCertificate;
}
