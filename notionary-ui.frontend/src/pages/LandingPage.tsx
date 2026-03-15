import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Gift, Globe, Lock, Users, ChevronRight } from 'lucide-react';

const features = [
  {
    icon: Gift,
    title: 'Curate your wishlists',
    desc: 'Organize items from anywhere on the web — links, prices, images and descriptions in one place.',
  },
  {
    icon: Globe,
    title: 'Share publicly or privately',
    desc: 'Make lists public for anyone to view, or keep them private and invite specific people by email.',
  },
  {
    icon: Users,
    title: 'Secret coordination',
    desc: 'Friends can discuss and reserve items without spoiling the surprise for the wishlist owner.',
  },
  {
    icon: Lock,
    title: 'Private by default',
    desc: 'Your wishlists are private until you decide to share them. Full control, always.',
  },
];

const stagger = {
  animate: { transition: { staggerChildren: 0.08 } },
};

const fadeUp = {
  initial: { opacity: 0, y: 20 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-white">
      {/* Navbar */}
      <nav className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-gray-100">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 flex items-center justify-between h-14">
          <div className="flex items-center gap-2 font-bold text-gray-900">
            <Gift size={20} className="text-violet-600" />
            <span>Notionary</span>
          </div>
          <div className="flex items-center gap-2">
            <Link
              to="/sign-in"
              className="px-3 py-1.5 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500"
            >
              Sign in
            </Link>
            <Link
              to="/sign-up"
              className="px-3 py-1.5 text-sm font-medium text-white bg-violet-600 rounded-xl hover:bg-violet-700 active:bg-violet-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 focus-visible:ring-offset-2"
            >
              Get started free
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="max-w-5xl mx-auto px-4 sm:px-6 pt-20 pb-16 text-center">
        <motion.div
          variants={stagger}
          initial="initial"
          animate="animate"
          className="flex flex-col items-center"
        >
          <motion.div
            variants={fadeUp}
            className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-violet-50 border border-violet-200 text-violet-700 text-sm font-medium mb-6"
          >
            <Gift size={14} />
            Wishlist management, reimagined
          </motion.div>

          <motion.h1
            variants={fadeUp}
            className="text-4xl sm:text-6xl font-bold text-gray-900 leading-tight tracking-tight max-w-3xl"
          >
            Your wishlists,{' '}
            <span className="text-violet-600">beautifully organized</span>
          </motion.h1>

          <motion.p
            variants={fadeUp}
            className="mt-5 text-lg text-gray-500 max-w-xl leading-relaxed"
          >
            Create and share wishlists for any occasion. Let friends secretly coordinate
            gifts without spoiling the surprise.
          </motion.p>

          <motion.div variants={fadeUp} className="flex flex-col sm:flex-row gap-3 mt-8">
            <Link
              to="/sign-up"
              className="inline-flex items-center justify-center gap-2 px-6 py-3 text-base font-semibold text-white bg-violet-600 rounded-xl hover:bg-violet-700 active:bg-violet-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 focus-visible:ring-offset-2"
            >
              Create your first wishlist
              <ChevronRight size={18} />
            </Link>
            <Link
              to="/sign-in"
              className="inline-flex items-center justify-center gap-2 px-6 py-3 text-base font-semibold text-gray-700 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-300"
            >
              Sign in
            </Link>
          </motion.div>
        </motion.div>
      </section>

      {/* Features */}
      <section className="max-w-5xl mx-auto px-4 sm:px-6 py-16">
        <motion.div
          variants={stagger}
          initial="initial"
          whileInView="animate"
          viewport={{ once: true }}
          className="grid sm:grid-cols-2 lg:grid-cols-4 gap-5"
        >
          {features.map((f) => (
            <motion.div
              key={f.title}
              variants={fadeUp}
              className="p-5 rounded-2xl bg-gray-50 border border-gray-100 hover:border-violet-200 hover:bg-violet-50/30 transition-colors group"
            >
              <div className="w-10 h-10 rounded-xl bg-violet-100 flex items-center justify-center mb-3 group-hover:bg-violet-200 transition-colors">
                <f.icon size={18} className="text-violet-600" />
              </div>
              <h3 className="font-semibold text-gray-900 text-sm mb-1.5">{f.title}</h3>
              <p className="text-xs text-gray-500 leading-relaxed">{f.desc}</p>
            </motion.div>
          ))}
        </motion.div>
      </section>

      {/* CTA */}
      <section className="max-w-5xl mx-auto px-4 sm:px-6 py-16">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.4 }}
          className="bg-gradient-to-br from-violet-600 to-purple-700 rounded-3xl p-10 text-center text-white"
        >
          <h2 className="text-2xl sm:text-3xl font-bold mb-3">
            Ready to share your wishes?
          </h2>
          <p className="text-violet-200 mb-6 text-base">
            Join thousands of people who use Notionary to organize their wishlists.
          </p>
          <Link
            to="/sign-up"
            className="inline-flex items-center gap-2 px-6 py-3 bg-white text-violet-700 font-semibold rounded-xl hover:bg-violet-50 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-violet-600"
          >
            Get started for free
            <ChevronRight size={16} />
          </Link>
        </motion.div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-100 py-8 text-center text-sm text-gray-400">
        <div className="flex items-center justify-center gap-2">
          <Gift size={14} className="text-violet-400" />
          <span>Notionary — wishlist management</span>
        </div>
      </footer>
    </div>
  );
}
