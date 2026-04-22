import Placeholder from '@/components/layout/Placeholder'
import { useParams } from 'react-router-dom'
export default function PayrollDetailPage() {
  const { id } = useParams()
  return <Placeholder title={`급여 상세 #${id ?? ''}`} />
}
