import type { MenuProps } from 'ant-design-vue'

/**
 * 顶部导航菜单配置
 * - key: 用作 Menu key，同时用于路由跳转（以 / 开头时）
 * - label: 展示文案
 */
export const globalHeaderMenus: NonNullable<MenuProps['items']> = [
  {
    key: '/',
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
]

