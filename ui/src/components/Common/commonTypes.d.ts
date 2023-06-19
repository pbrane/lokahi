interface ItemStatus {
    title: string;
    status: string;
    statusColor: string;
    statusText: string;
}

interface ItemPreviewProps {
    loading: boolean;
    title: string;
    itemTitle: string;
    itemSubtitle: string;
    itemStatuses: ItemStatus[];
}