import { FindMinionsByLocationIdDocument } from '@/types/graphql'
import { QueryService } from '.'

export const getMinionsByLocationId = (locationId: string) => {

  const queryOptions = {
    query: FindMinionsByLocationIdDocument,
    cachePolicy: 'network-only',
    variables: { locationId }
  }

  const notificationOptions = {
    notifyOnError: true,
    notifyOnSuccess: true,
    errorMessage: 'Minions cannot be located',
    successMessage: 'Minions Located'
  }

  return QueryService.executeQuery<{ locationId: string }>(queryOptions, notificationOptions)
}
