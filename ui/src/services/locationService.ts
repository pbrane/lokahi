import { FindLocationsForWelcomeDocument } from "@/types/graphql"
import { QueryService } from "."

export interface WelcomeLocations {
    id: number;
    location: string;
}
export const getLocationsForWelcome = async (): Promise<WelcomeLocations[]> => {
      const data = await QueryService.executeQuery({ query: FindLocationsForWelcomeDocument })
      return toRaw(data?.findAllLocations) || [];
}