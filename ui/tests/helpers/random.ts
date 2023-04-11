import casual from 'casual'

export const rndNumber = () => Math.floor(Math.random() * 100)

export const rndStatus = () => casual.random_element(['UP', 'DOWN'])

export const rndLatency = () => casual.random_element(['OK', 'FAILED', 'UNKNOWN'])

export const rndUptime = () => casual.random_element(['OK', 'FAILED', 'UNKNOWN'])

export const rndSeverity = () => casual.random_element(['CRITICAL', 'MAJOR', 'MINOR', 'INDETERMINATE'])

export const rndDuration = () => casual.random_element(['3hrs', '1day', '1week', '1month'])

export const rndNodeType = () => casual.random_element(['Server', 'Switch', 'Router'])
