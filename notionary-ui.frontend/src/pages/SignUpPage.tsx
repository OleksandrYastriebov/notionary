import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { motion, AnimatePresence } from 'framer-motion';
import { Gift, Mail, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import { useState, useEffect } from 'react';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { signUp, resendConfirmationEmail } from '../api/endpoints';

const schema = z.object({
  firstName: z.string().min(1, 'First name is required').max(50),
  lastName: z.string().min(1, 'Last name is required').max(50),
  email: z.string().email('Enter a valid email'),
  password: z
    .string()
    .min(8, 'Password must be at least 8 characters')
    .max(100),
});

type FormData = z.infer<typeof schema>;

const RESEND_COOLDOWN = 60;

export default function SignUpPage() {
  const [success, setSuccess] = useState(false);
  const [registeredEmail, setRegisteredEmail] = useState('');
  const [countdown, setCountdown] = useState(0);
  const [resending, setResending] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  // Countdown timer
  useEffect(() => {
    if (countdown <= 0) return;
    const id = setInterval(() => setCountdown((c) => c - 1), 1000);
    return () => clearInterval(id);
  }, [countdown]);

  const onSubmit = async (data: FormData) => {
    try {
      await signUp(data);
      setRegisteredEmail(data.email);
      setSuccess(true);
      setCountdown(RESEND_COOLDOWN);
    } catch {
      toast.error('Sign up failed. The email may already be in use.');
    }
  };

  const handleResend = async () => {
    if (countdown > 0 || resending) return;
    setResending(true);
    try {
      await resendConfirmationEmail(registeredEmail);
      toast.success('Confirmation email resent!');
      setCountdown(RESEND_COOLDOWN);
    } catch {
      toast.error('Failed to resend email. Try again later.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35 }}
        className="w-full max-w-sm"
      >
        {/* Logo */}
        <div className="flex justify-center mb-8">
          <Link
            to="/"
            className="flex items-center gap-2 font-bold text-gray-900 hover:text-violet-600 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 rounded-lg"
          >
            <Gift size={22} className="text-violet-600" />
            <span className="text-lg">Notionary</span>
          </Link>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <AnimatePresence mode="wait">
            {success ? (
              /* Success state */
              <motion.div
                key="success"
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.25 }}
                className="text-center"
              >
                <div className="w-14 h-14 rounded-2xl bg-violet-100 flex items-center justify-center mx-auto mb-4">
                  <Mail size={26} className="text-violet-600" />
                </div>
                <h2 className="text-xl font-bold text-gray-900 mb-2">Check your email</h2>
                <p className="text-sm text-gray-500 leading-relaxed mb-6">
                  We&apos;ve sent a confirmation link to{' '}
                  <strong className="text-gray-700">{registeredEmail}</strong>. Click it
                  to activate your account.
                </p>

                <Button
                  variant="secondary"
                  onClick={() => void handleResend()}
                  disabled={countdown > 0 || resending}
                  isLoading={resending}
                  leftIcon={<RefreshCw size={14} />}
                  className="w-full"
                >
                  {countdown > 0
                    ? `Resend in ${countdown}s`
                    : 'Resend confirmation email'}
                </Button>

                <p className="text-sm text-gray-500 text-center mt-5">
                  Already confirmed?{' '}
                  <Link
                    to="/sign-in"
                    className="text-violet-600 font-medium hover:text-violet-700"
                  >
                    Sign in
                  </Link>
                </p>
              </motion.div>
            ) : (
              /* Sign up form */
              <motion.div
                key="form"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.2 }}
              >
                <h1 className="text-xl font-bold text-gray-900 mb-1">Create account</h1>
                <p className="text-sm text-gray-500 mb-6">
                  Start organizing your wishlists today
                </p>

                <form
                  onSubmit={(e) => void handleSubmit(onSubmit)(e)}
                  className="space-y-3"
                >
                  <div className="grid grid-cols-2 gap-3">
                    <Input
                      label="First name"
                      autoComplete="given-name"
                      placeholder="Alex"
                      error={errors.firstName?.message}
                      {...register('firstName')}
                    />
                    <Input
                      label="Last name"
                      autoComplete="family-name"
                      placeholder="Smith"
                      error={errors.lastName?.message}
                      {...register('lastName')}
                    />
                  </div>
                  <Input
                    label="Email"
                    type="email"
                    autoComplete="email"
                    placeholder="you@example.com"
                    error={errors.email?.message}
                    {...register('email')}
                  />
                  <Input
                    label="Password"
                    type="password"
                    autoComplete="new-password"
                    placeholder="Min 8 characters"
                    error={errors.password?.message}
                    {...register('password')}
                  />

                  <Button
                    type="submit"
                    className="w-full"
                    size="lg"
                    isLoading={isSubmitting}
                  >
                    Create account
                  </Button>
                </form>

                <p className="text-sm text-gray-500 text-center mt-5">
                  Already have an account?{' '}
                  <Link
                    to="/sign-in"
                    className="text-violet-600 font-medium hover:text-violet-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 rounded"
                  >
                    Sign in
                  </Link>
                </p>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </motion.div>
    </div>
  );
}
