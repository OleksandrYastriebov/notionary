import { Plus } from 'lucide-react'

const EmptyItemCard = ({ onClick }) => {
  return (
    <button
      onClick={onClick}
      className="card hover:shadow-lg transition-all duration-200 cursor-pointer border-2 border-dashed border-gray-300 hover:border-primary-400 bg-gray-50 hover:bg-primary-50 group h-full"
    >
      {/* Same aspect ratio as item cards */}
      <div className="aspect-square rounded-lg bg-gradient-to-br from-gray-100 to-gray-200 group-hover:from-primary-100 group-hover:to-primary-200 flex items-center justify-center mb-4 transition-colors">
        <Plus className="w-20 h-20 text-gray-400 group-hover:text-primary-600 transition-colors" />
      </div>
      
      <div className="text-center">
        <h3 className="text-lg font-semibold text-gray-700 group-hover:text-primary-700 mb-1 transition-colors">
          Add New Item
        </h3>
        <p className="text-sm text-gray-500 group-hover:text-primary-600 transition-colors">
          Click to add an item
        </p>
      </div>
    </button>
  )
}

export default EmptyItemCard