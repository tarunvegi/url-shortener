import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { urlApi, UrlInfo } from '../api/client'
import { BarChart2, Trash2, ExternalLink } from 'lucide-react'

export default function Manage() {
  const [urls, setUrls] = useState<UrlInfo[]>([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    fetchUrls()
  }, [])

  async function fetchUrls() {
    try {
      const data = await urlApi.listAll()
      setUrls(data)
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(code: string) {
    if (!confirm('Delete this short URL?')) return
    await urlApi.delete(code)
    setUrls(urls.filter(u => u.code !== code))
  }

  if (loading) return <p className="text-center text-gray-500 mt-10">Loading...</p>

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Manage URLs</h1>

      {urls.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          No URLs yet. <a href="/" className="text-blue-600 hover:underline">Create one</a>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Short URL</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Original URL</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Created</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {urls.map(url => (
                <tr key={url.code} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <a
                      href={url.shortUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="text-blue-600 hover:underline flex items-center gap-1"
                    >
                      {url.code}
                      <ExternalLink size={12} />
                    </a>
                  </td>
                  <td className="px-4 py-3 max-w-xs">
                    <span className="truncate block text-gray-600" title={url.originalUrl}>
                      {url.originalUrl}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500">
                    {new Date(url.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                      url.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'
                    }`}>
                      {url.active ? 'Active' : 'Expired'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2 justify-end">
                      <button
                        onClick={() => navigate(`/analytics/${url.code}`)}
                        className="p-1 text-gray-400 hover:text-blue-600 transition-colors"
                        title="View analytics"
                      >
                        <BarChart2 size={16} />
                      </button>
                      <button
                        onClick={() => handleDelete(url.code)}
                        className="p-1 text-gray-400 hover:text-red-600 transition-colors"
                        title="Delete"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
