export interface ServiceInventoryItem {
  id: number;
  type: string;
  service: string;
  node: number;
  description: string;
  reachability: string;
  latency: string;
  uptime: string;
}
interface SelectItem {
  id: number
  name: string
}
