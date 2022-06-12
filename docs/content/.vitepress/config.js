import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Scaffolding',
  description: 'Schematic library for Minestom',

  themeConfig: {
    sidebar: {
      '/': getSidebar()
    }
  }
})

function getSidebar() {
  return [
    {
      text: 'Introduction',
      link: '/'
    },
    {
      text: 'Setup',
      children: [
        { text: 'Getting Started', link: '/setup/getting-started' },
      ]
    },
    {
      text: 'Using Schematics',
      children: [
        { text: 'Load', link: '/usage/read' },
        { text: 'Copy', link: '/usage/copy' },
        { text: 'Build', link: '/usage/build ' },
        { text: 'Write', link: '/usage/write ' },
      ]
    }, 
    {
      text: 'Utilities',
      children: [
        { text: 'Region', link: '/utilities/region' },
      ]
    },
  ]
}
