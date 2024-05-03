import { CountAlertByPolicyIdDocument, CountAlertByRuleIdDocument, ListMonitoryPoliciesDocument, ListVendorDocument } from '@/types/graphql'
import { defineStore } from 'pinia'
import { useQuery } from 'villus'

export const useMonitoringPoliciesQueries = defineStore('monitoringPoliciesQueries', () => {
  const { data, execute: listMonitoringPolicies } = useQuery({
    query: ListMonitoryPoliciesDocument,
    cachePolicy: 'network-only'
  })

  const monitoringPolicies = computed(() => {
    if (!data.value) return []

    const policies = data.value?.listMonitoryPolicies || []

    if (data.value.defaultPolicy) {
      return [{ ...data.value.defaultPolicy, isDefault: true }, ...policies]
    }

    return policies
  })

  const getAlertCountByPolicyId = async (id: number) => {
    const { execute, data } = useQuery({
      query: CountAlertByPolicyIdDocument,
      variables: { id },
      cachePolicy: 'network-only'
    })
    await execute()
    return data.value?.countAlertByPolicyId
  }

  const getAlertCountByRuleId = async (id: number) => {
    const { execute, data } = useQuery({
      query: CountAlertByRuleIdDocument,
      variables: { id },
      cachePolicy: 'network-only'
    })
    await execute()
    return data.value?.countAlertByRuleId
  }

  const listVendors = async () => {
    const { execute, data } = useQuery({
      query: ListVendorDocument,
      cachePolicy: 'network-only'
    })
    await execute()
    return data.value?.listVendors
  }

  return {
    monitoringPolicies,
    listMonitoringPolicies,
    getAlertCountByPolicyId,
    getAlertCountByRuleId,
    listVendors
  }
})
