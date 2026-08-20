import { useState } from 'react'
import { urlApi, ShortenResponse } from '../api/client'
import { Link2, Copy, Check, ChevronDown, ChevronUp } from 'lucide-react'

function Toast({ show }: { show: boolean }) {
  return (
    <div className={`fixed bottom-6 right-6 flex items-center gap-2 bg-gray-900 text-white px-4 py-3 rounded-lg shadow-lg transition-all duration-300 ${show ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4 pointer-events-none'}`}>
      <Check size={16} className="text-green-400" />
      <span className="text-sm font-medium">Copied to clipboard!</span>
    </div>
  )
}

export default function Home() {
  const [url, setUrl] = useState('')
  const [alias, setAlias] = useState('')
  const [expiresAt, setExpiresAt] = useState('')
  const [showOptions, setShowOptions] = useState(false)
  const [result, setResult] = useState<ShortenResponse | null>(null)
  const [copied, setCopied] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleShorten(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setResult(null)
    setLoading(true)
    try {
      const response = await urlApi.shorten({
        originalUrl: url,
        customAlias: alias || undefined,
        expiresAt: expiresAt || undefined
      })
      setResult(response)
      setUrl('')
      setAlias('')
      setExpiresAt('')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }

  async function copyToClipboard() {
    if (!result) return
    await navigator.clipboard.writeText(result.shortUrl)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="max-w-2xl mx-auto">
      <Toast show={copied} />
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-2">Shorten Your URL</h1>
        <p className="text-gray-500">Paste a long link, get a short one instantly</p>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <form onSubmit={handleShorten} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Long URL</label>
            <input
              type="url"
              value={url}
              onChange={e => setUrl(e.target.value)}
              placeholder="https://www.example.com/very/long/url"
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
            type="button"
            onClick={() => setShowOptions(!showOptions)}
            className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
          >
            {showOptions ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
            Advanced options
          </button>

          {showOptions && (
            <div className="space-y-3 border-t pt-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Custom Alias (optional)</label>
                <div className="flex items-center">
                  <span className="px-3 py-2 bg-gray-100 border border-r-0 border-gray-300 rounded-l-lg text-gray-500 text-sm">short.ly/</span>
                  <input
                    type="text"
                    value={alias}
                    onChange={e => setAlias(e.target.value)}
                    placeholder="my-link"
                    pattern="[a-zA-Z0-9_-]*"
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-r-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Expiry Date (optional)</label>
                <input
                  type="datetime-local"
                  value={expiresAt}
                  onChange={e => setExpiresAt(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          )}

          {error && (
            <div className="bg-red-50 text-red-700 px-4 py-3 rounded-lg text-sm">{error}</div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {loading ? 'Shortening...' : 'Shorten URL'}
          </button>
        </form>

        {result && (
          <div className="mt-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
            <p className="text-sm text-gray-500 mb-1">Your short link</p>
            <div className="flex items-center gap-2">
              <a
                href={result.shortUrl}
                target="_blank"
                rel="noreferrer"
                className="flex-1 text-blue-600 font-semibold hover:underline flex items-center gap-1"
              >
                <Link2 size={16} />
                {result.shortUrl}
              </a>
              <button
                onClick={copyToClipboard}
                className="flex items-center gap-1 px-3 py-1 bg-white border border-gray-300 rounded-md text-sm hover:bg-gray-50 transition-colors"
              >
                {copied ? <Check size={14} className="text-green-600" /> : <Copy size={14} />}
                {copied ? 'Copied!' : 'Copy'}
              </button>
            </div>
            {result.expiresAt && (
              <p className="text-xs text-gray-400 mt-1">Expires: {new Date(result.expiresAt).toLocaleString()}</p>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
