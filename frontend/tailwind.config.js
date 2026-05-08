/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts,scss}'],
  theme: {
    extend: {
      colors: {
        carbon: {
          950: '#0E0E0E',
          900: '#141414',
          800: '#1A1A1A',
          700: '#202020',
          600: '#242424',
          500: '#2E2E2E',
        },
        ink: {
          DEFAULT: '#F0EDE8',
          muted: '#585450',
          sand: '#8A7060',
        },
        coral: {
          DEFAULT: '#FF5A35',
          light: '#FF7A5A',
          dim: 'rgba(255,90,53,0.1)',
        },
        warm: '#FFB38A',
      },
      fontFamily: {
        display: ['Syne', 'sans-serif'],
        mono:    ['Roboto Mono', 'monospace'],
      },
      boxShadow: {
        'glow-coral': '0 0 20px rgba(255,90,53,0.2), 0 0 60px rgba(255,90,53,0.05)',
        'glow-sm':    '0 0 8px rgba(255,90,53,0.25)',
      },
    },
  },
  plugins: [],
};
