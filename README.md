## NestedRecyclerView

### 仿淘宝、京东首页，通过两层嵌套的RecyclerView实现tab的吸顶效果
[文章链接](https://juejin.im/post/5d5f4cfcf265da03e61b18b8)

项目gif展示：

![](./gif/nested_recyclerview_1.gif)

![](./gif/nested_recyclerview_2.gif)

### 大致实现方式
![](https://raw.githubusercontent.com/JasonGaoH/Images/master/nested_recycler_view.png)

- 外部RecyclerView为ParentRecyclerView，将需要吸顶的悬浮效果的TabLayout和ViewPager作为ParentRecyclerView的一个item。
- 内部RecyclerView为ChildRecyclerView,ParentRecyclerView和ChildRecyclerView相互协调:
  - 当ParentRecyclerView滚动到底部的时候，让ChildRecyclerView去滚动
  - 当ChildRecyclerView滚动到顶部的时候，让ParentRecyclerView去滚动
  - fling效果与滑动类似
- 具体实现可看项目代码

### 新增Tab折叠动画
![](https://raw.githubusercontent.com/JasonGaoH/NestedRecyclerView/master/gif/nested_recyclerview_animaion.gif)

关于
--

博客：[https://blog.csdn.net/H_Gao](https://blog.csdn.net/H_Gao)

邮箱：532294580@qq.com

License
--
Copyright 2018 JasonGaoH

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
