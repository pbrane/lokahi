import { FindMinionsByLocationIdDocument, ListMinionsForTableDocument } from '@/types/graphql'
import { QueryService } from '.'
import { useQuery } from 'villus'

export const getMinionsByLocationId = async (locationId: number) => {

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

  const queryResults = await QueryService.executeQuery<{ locationId: number }>(queryOptions, notificationOptions)

  return toRaw(queryResults).findMinionsByLocationId;
}

export const getAllMinions = async (): Promise<[]> => {

  const { execute } = useQuery({
    query: ListMinionsForTableDocument,
    cachePolicy: 'network-only'
  });

  const allMinions = await execute();
  const rawResult = toRaw(allMinions.data)?.findAllMinions || []
  return allMinions.error ? [] : rawResult
}