import { DownloadMinionCertificateForWelcomeDocument } from "@/types/graphql";
import { defineStore } from "pinia";
import { useQuery } from "villus";


export const useCertificateQueries = defineStore('certificateQueries', () => {

    const getMinionCertificate = async (locationId: number) => {

        const { execute } = useQuery({
            query: DownloadMinionCertificateForWelcomeDocument,
            cachePolicy: 'network-only',
            fetchOnMount: false,
            variables: { location: locationId }
        })
        const cert = await execute();
        return cert.data.getMinionCertificate;
    }

    return { getMinionCertificate };
})