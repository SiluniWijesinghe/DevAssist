import { LoaderCircle } from 'lucide-react';

export default function Spinner({ size = 15 }) {
  return <LoaderCircle size={size} className="animate-spin motion-reduce:[animation-duration:3s]" aria-hidden="true" />;
}
