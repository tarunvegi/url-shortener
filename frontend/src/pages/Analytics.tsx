import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { urlApi, AnalyticsData } from '../api/client'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts'
import { ArrowLeft } from 'lucide-react'

const PIE_COLORS = ['#6366f1', '#22c55e', '#f59e0b', '#ef4444', '#14b8a6']

export default function Analytics() {
  const { code } = useParams<{ code: string }>()
  const navigate = useNavigate()
  const [data, setData] = useState<AnalyticsData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!code) return
    urlApi.getAnalytics(code)
      .then(setData)
      .catch(() => setError('Could not load analytics'))
      .finally(() => setLoading(false))
  }, [code])

  if (loading) return <p className="text-center text-gray-500 mt-10">Loading analytics...</p>
  if (error) return <p className="text-center text-red-500 mt-10">{error}</p>
  if (!data) return null

  const chartData = Object.entries(data.clicksByDay).map(([date, count]) => ({ date, clicks: count }))
  const referrerPieData = Object.entries(data.topReferrers).map(([name, value]) => ({
    name: name || 'Direct',
    value,
  }))

  return (
    <div>
      <button
        onClick={() => navigate('/manage')}
        className="flex items-center gap-1 text-gray-500 hover:text-gray-800 mb-6 text-sm"
      >
        <ArrowLeft size={16} /> Back to Manage
      </button>

      <h1 className="text-2xl font-bold text-gray-800 mb-2">
        Analytics: <span className="text-blue-600">{code}</span>
      </h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm text-center">
          <p className="text-4xl font-bold text-blue-600">{data.totalClicks.toLocaleString()}</p>
          <p className="text-gray-500 text-sm mt-1">Total Clicks</p>
        </div>
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm text-center">
          <p className="text-4xl font-bold text-blue-600">{Object.keys(data.clicksByDay).length}</p>
          <p className="text-gray-500 text-sm mt-1">Active Days</p>
        </div>
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm text-center">
          <p className="text-4xl font-bold text-blue-600">{Object.keys(data.topReferrers).length}</p>
          <p className="text-gray-500 text-sm mt-1">Referrer Sources</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm mb-6">
        <h2 className="text-lg font-semibold text-gray-700 mb-4">Clicks Per Day (Last 30 Days)</h2>
        {chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Line type="monotone" dataKey="clicks" stroke="#2563eb" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <p className="text-gray-400 text-center py-8">No click data yet</p>
        )}
      </div>

      {referrerPieData.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-gray-700 mb-4">Referrer Breakdown</h2>
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie
                  data={referrerPieData}
                  cx="50%"
                  cy="45%"
                  innerRadius={55}
                  outerRadius={90}
                  dataKey="value"
                  paddingAngle={3}
                >
                  {referrerPieData.map((_, index) => (
                    <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value, name) => [`${value} clicks`, name]} />
                <Legend iconType="circle" iconSize={10} />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-gray-700 mb-4">Top Referrers</h2>
            <div className="space-y-3">
              {Object.entries(data.topReferrers).map(([referrer, count], index) => {
                const total = Object.values(data.topReferrers).reduce((a, b) => a + b, 0)
                const pct = total > 0 ? Math.round((count / total) * 100) : 0
                return (
                  <div key={referrer}>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-gray-600 truncate max-w-[200px]">{referrer || 'Direct'}</span>
                      <span className="font-semibold text-blue-600 ml-2">{count} ({pct}%)</span>
                    </div>
                    <div className="w-full bg-gray-100 rounded-full h-2">
                      <div
                        className="h-2 rounded-full"
                        style={{ width: `${pct}%`, backgroundColor: PIE_COLORS[index % PIE_COLORS.length] }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
