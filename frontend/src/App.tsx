import { Routes, Route, Link, useLocation } from 'react-router-dom'
import Home from './pages/Home'
import Analytics from './pages/Analytics'
import Manage from './pages/Manage'

export default function App() {
  const location = useLocation()

  const navLink = (to: string, label: string) => (
    <Link
      to={to}
      className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
        location.pathname === to
          ? 'bg-blue-600 text-white'
          : 'text-gray-600 hover:text-blue-600'
      }`}
    >
      {label}
    </Link>
  )

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200 shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <span className="text-xl font-bold text-blue-600">URL Shortener</span>
          <div className="flex gap-2">
            {navLink('/', 'Home')}
            {navLink('/manage', 'Manage')}
          </div>
        </div>
      </nav>
      <main className="max-w-4xl mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/manage" element={<Manage />} />
          <Route path="/analytics/:code" element={<Analytics />} />
        </Routes>
      </main>
    </div>
  )
}
