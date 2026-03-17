import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { motion, AnimatePresence } from 'framer-motion';
import { Sparkles, Globe, Lock } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { useGenerateWishlist } from '../../hooks/useWishlists';

const schema = z.object({
  description: z
    .string()
    .min(10, 'Description must be at least 10 characters')
    .max(500, 'Description must not exceed 500 characters'),
  isPublic: z.boolean(),
});

type FormValues = z.infer<typeof schema>;

interface AiGenerateModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function AiGenerateModal({ isOpen, onClose }: AiGenerateModalProps) {
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { description: '', isPublic: false },
  });

  const description = watch('description');
  const isPublic = watch('isPublic');
  const charsLeft = description.length;
  const isNearLimit = charsLeft > 450;

  const generateMutation = useGenerateWishlist(() => {
    reset();
    onClose();
  });

  useEffect(() => {
    if (!isOpen) {
      reset();
      generateMutation.reset();
    }
    // generateMutation.reset is intentionally excluded to avoid infinite loops
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen]);

  const onSubmit = (values: FormValues) => {
    generateMutation.mutate(values);
  };

  return (
    <Modal isOpen={isOpen} onClose={generateMutation.isPending ? () => {} : onClose} size="md">
      <div className="relative overflow-hidden">
        {/* Loading overlay */}
        <AnimatePresence>
          {generateMutation.isPending && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="absolute inset-0 z-20 flex flex-col items-center justify-center gap-4 bg-white/80 backdrop-blur-sm rounded-2xl"
            >
              <div className="relative">
                <div className="w-12 h-12 rounded-full border-[3px] border-violet-200 border-t-violet-600 animate-spin" />
                <Sparkles size={16} className="absolute inset-0 m-auto text-violet-600" />
              </div>
              <p className="text-sm font-medium text-gray-700">Generating your wishlist...</p>
              <p className="text-xs text-gray-400">This may take a few seconds</p>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Gradient header */}
        <div className="bg-gradient-to-br from-violet-600 to-purple-700 px-6 py-5 flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center flex-shrink-0">
            <Sparkles size={18} className="text-white" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-white">Generate with AI</h2>
            <p className="text-xs text-violet-200 mt-0.5">
              Describe your wishlist and AI will create it for you
            </p>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="p-6 flex flex-col gap-5">
          {/* Textarea */}
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">
              Describe your wishlist
            </label>
            <textarea
              {...register('description')}
              rows={4}
              maxLength={500}
              placeholder="e.g. Birthday wishlist for a tech enthusiast who loves gaming and smart home devices, budget around $500..."
              className="w-full resize-none rounded-xl border border-gray-200 px-3.5 py-3 text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent transition"
            />
            <div className="flex items-start justify-between gap-2">
              {errors.description ? (
                <p className="text-xs text-red-500">{errors.description.message}</p>
              ) : (
                <span />
              )}
              <span
                className={`text-xs tabular-nums flex-shrink-0 ${isNearLimit ? 'text-red-500' : 'text-gray-400'}`}
              >
                {charsLeft} / 500
              </span>
            </div>
          </div>

          {/* Visibility toggle */}
          <div className="flex items-center gap-3">
            <span className="text-sm font-medium text-gray-700">Visibility</span>
            <div className="flex rounded-lg border border-gray-200 overflow-hidden">
              <button
                type="button"
                onClick={() => setValue('isPublic', false)}
                className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium transition-colors ${
                  !isPublic
                    ? 'bg-violet-600 text-white'
                    : 'bg-white text-gray-500 hover:bg-gray-50'
                }`}
              >
                <Lock size={12} />
                Private
              </button>
              <button
                type="button"
                onClick={() => setValue('isPublic', true)}
                className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium transition-colors ${
                  isPublic
                    ? 'bg-violet-600 text-white'
                    : 'bg-white text-gray-500 hover:bg-gray-50'
                }`}
              >
                <Globe size={12} />
                Public
              </button>
            </div>
          </div>

          {/* Generate button */}
          <button
            type="submit"
            disabled={generateMutation.isPending}
            className="w-full flex items-center justify-center gap-2 px-5 py-3 rounded-xl font-semibold text-sm text-white bg-gradient-to-r from-violet-600 to-purple-600 hover:to-purple-700 active:from-violet-700 active:to-purple-800 transition-all disabled:opacity-60 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 focus-visible:ring-offset-2 shadow-md shadow-violet-200"
          >
            <Sparkles size={15} />
            Generate Wishlist
          </button>
        </form>
      </div>
    </Modal>
  );
}
