/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/**/*.{html,ts}',
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#f0f4ff',
          100: '#e0e9ff',
          200: '#c7d7fe',
          300: '#a5bbfc',
          400: '#8196f8',
          500: '#6271f1',
          600: '#4f52e5',
          700: '#4240ca',
          800: '#3637a3',
          900: '#303282',
          950: '#1e1c4b',
        },
      },
    },
  },
  plugins: [],
};
