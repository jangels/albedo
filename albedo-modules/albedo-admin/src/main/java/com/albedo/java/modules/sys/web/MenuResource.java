/*
 *  Copyright (c) 2019-2020, somewhere (somewhere0813@gmail.com).
 *  <p>
 *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 * https://www.gnu.org/licenses/lgpl.html
 *  <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.albedo.java.modules.sys.web;

import com.albedo.java.common.core.constant.CommonConstants;
import com.albedo.java.common.core.util.CollUtil;
import com.albedo.java.common.core.util.R;
import com.albedo.java.common.core.vo.PageModel;
import com.albedo.java.common.core.vo.TreeQuery;
import com.albedo.java.common.core.vo.TreeUtil;
import com.albedo.java.common.log.annotation.Log;
import com.albedo.java.common.log.enums.BusinessType;
import com.albedo.java.common.security.util.SecurityUtil;
import com.albedo.java.common.web.resource.BaseResource;
import com.albedo.java.modules.sys.domain.Menu;
import com.albedo.java.modules.sys.domain.dto.MenuSortDto;
import com.albedo.java.modules.sys.domain.vo.MenuTree;
import com.albedo.java.modules.sys.domain.vo.MenuVo;
import com.albedo.java.modules.sys.service.MenuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author somewhere
 * @date 2019/2/1
 */
@RestController
@RequestMapping("${application.admin-path}/sys/menu")
@AllArgsConstructor
public class MenuResource  extends BaseResource {

	private final MenuService menuService;

	/**
	 * @param id
	 * @return
	 */
	@GetMapping(CommonConstants.URL_ID_REGEX)
	public R get(@PathVariable String id) {
		log.debug("REST request to get Entity : {}", id);
		return R.buildOkData(menuService.getOneDto(id));
	}

	/**
	 * 返回当前用户的树形菜单集合
	 *
	 * @return 当前用户的树形菜单
	 */
	@GetMapping("/user-menu")
	public R getUserMenu() {
		// 获取符合条件的菜单
		Set<MenuVo> all = new HashSet<>();
		SecurityUtil.getRoles()
			.forEach(roleId -> all.addAll(menuService.getMenuByRoleId(roleId)));
		List<MenuTree> menuTreeList = all.stream()
			.filter(menuVo -> Menu.TYPE_MENU.equals(menuVo.getType()))
			.sorted(Comparator.comparingInt(MenuVo::getSort))
			.map(MenuTree::new)
			.collect(Collectors.toList());
		return R.buildOkData(buildMenus(Lists.newArrayList(TreeUtil.buildByLoopAutoRoot(menuTreeList))));
	}


	/**
	 * 两层循环实现建树
	 *
	 * @param menuTreeList 传入的树节点列表
	 * @return
	 */
	public List<MenuTree> buildMenus(List<MenuTree> menuTreeList) {
		menuTreeList.forEach(menu -> {
				if (menu!=null){
					List<MenuTree> menuChildList = menu.getChildren();
					if(CollUtil.isNotEmpty(menuChildList)){
						menu.setAlwaysShow(true);
						menu.setRedirect("noredirect");
						menu.setChildren(buildMenus(menuChildList));
						// 处理是一级菜单并且没有子菜单的情况
					} else if(menu.getParentId() == TreeUtil.ROOT){
						MenuTree menuVo = new MenuTree();
						menuVo.setMeta(menu.getMeta());
						// 非外链
						if(!CommonConstants.YES.equals(menu.getIFrame())){
							menuVo.setPath("index");
							menuVo.setName(menu.getName());
							menuVo.setComponent(menu.getComponent());
						} else {
							menuVo.setPath(menu.getPath());
						}
						menu.setName(null);
						menu.setMeta(null);
						menu.setComponent("Layout");
						menu.setChildren(Lists.newArrayList(menuVo));
					}
				}
			}
		);
		return menuTreeList;

	}


	/**
	 * 返回树形菜单集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/tree")
	public R getTree(TreeQuery treeQuery) {
		return R.buildOkData(menuService.listMenuTrees(treeQuery));
	}

	/**
	 * 返回角色的菜单集合
	 *
	 * @param roleId 角色ID
	 * @return 属性集合
	 */
	@GetMapping("/tree/{roleId}")
	public List getRoleTree(@PathVariable String roleId) {
		return menuService.getMenuByRoleId(roleId)
			.stream()
			.map(MenuVo::getId)
			.collect(Collectors.toList());
	}

	/**
	 * 新增菜单
	 *
	 * @param menuDto 菜单信息
	 * @return success/false
	 */
	@Log(value = "菜单管理", businessType = BusinessType.EDIT)
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_menu_edit')")
	public R save(@Valid @RequestBody com.albedo.java.modules.sys.domain.dto.MenuDto menuDto) {
		menuService.saveOrUpdate(menuDto);
		return R.buildOk("操作成功");
	}

	/**
	 * 更新菜单序号
	 *
	 * @param menuSortDto 菜单信息
	 * @return success/false
	 */
	@Log(value = "菜单管理", businessType = BusinessType.EDIT)
	@PostMapping("/sort-update")
	@PreAuthorize("@pms.hasPermission('sys_menu_edit')")
	public R sortUpdate(@Valid @RequestBody MenuSortDto menuSortDto) {
		menuService.sortUpdate(menuSortDto);
		return R.buildOk("操作成功");
	}

	/**
	 * 删除菜单
	 *
	 * @param ids 菜单ID
	 * @return success/false
	 */
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_menu_del')")
	@Log(value = "菜单管理", businessType = BusinessType.DELETE)
	public R removeByIds(@RequestBody Set<String> ids) {
		menuService.removeMenuById(ids);
		return R.buildOk("操作成功");
	}

	/**
	 * 分页查询菜单信息
	 *
	 * @param pm 分页对象
	 * @return 分页对象
	 */
	@GetMapping
	public R<IPage> getPage(PageModel pm) {
		return R.buildOkData(menuService.page(pm));
	}

}
