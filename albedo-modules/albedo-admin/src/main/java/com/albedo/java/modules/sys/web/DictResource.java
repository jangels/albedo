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
import com.albedo.java.common.core.util.Json;
import com.albedo.java.common.core.util.R;
import com.albedo.java.common.core.vo.PageModel;
import com.albedo.java.common.core.vo.SelectResult;
import com.albedo.java.common.log.annotation.Log;
import com.albedo.java.common.log.enums.BusinessType;
import com.albedo.java.common.web.resource.BaseResource;
import com.albedo.java.modules.sys.domain.Dict;
import com.albedo.java.modules.sys.domain.dto.DictDto;
import com.albedo.java.modules.sys.domain.dto.DictQueryCriteria;
import com.albedo.java.modules.sys.service.DictService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 字典表 前端控制器
 * </p>
 *
 * @author somewhere
 * @since 2019/2/1
 */
@RestController
@RequestMapping("${application.admin-path}/sys/dict")
@AllArgsConstructor
public class DictResource extends BaseResource {

	private final DictService dictService;

	/**
	 * 返回树形菜单集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/tree")
	public R tree(DictQueryCriteria dictQueryCriteria) {
		return R.buildOkData(dictService.findTreeList(dictQueryCriteria));
	}

	/**
	 * @param id
	 * @return
	 */
	@GetMapping(CommonConstants.URL_ID_REGEX)
	public R get(@PathVariable String id) {
		log.debug("REST request to get Entity : {}", id);
		return R.buildOkData(dictService.getOneDto(id));
	}

	/**
	 * 分页查询字典信息
	 *
	 * @param pm 分页对象
	 * @return 分页对象
	 */
	@GetMapping
	public R<IPage> getPage(PageModel pm) {
		return new R<>(dictService.page(pm));
	}

	/**
	 * 通过字典类型查找字典
	 *
	 * @param codes
	 * @return
	 */
	@ApiOperation(value = "获取字典数据", notes = "codes 不传获取所有的业务字典，多个用','隔开")
	@GetMapping(value = "/codes")
	public R getByCodes(String codes) {
		Map<String, List<SelectResult>> map = codes != null ?
			dictService.findCodes(codes) : dictService.findCodes();
		return new R<>(map);
	}

	/**
	 * 添加字典
	 *
	 * @param dictDto 字典信息
	 * @return success、false
	 */
	@PostMapping
	@CacheEvict(value = Dict.CACHE_DICT_DETAILS, allEntries = true)
	@PreAuthorize("@pms.hasPermission('sys_dict_edit')")
	@Log(value = "字典管理", businessType = BusinessType.EDIT)
	public R save(@Valid @RequestBody DictDto dictDto) {

		dictService.saveOrUpdate(dictDto);
		return R.buildOk("操作成功");
	}

	/**
	 * 删除字典，并且清除字典缓存
	 *
	 * @param ids ID
	 * @return R
	 */
	@DeleteMapping
	@CacheEvict(value = Dict.CACHE_DICT_DETAILS, allEntries = true)
	@PreAuthorize("@pms.hasPermission('sys_dict_del')")
	@Log(value = "字典管理", businessType = BusinessType.DELETE)
	public R removeByIds(@RequestBody Set<String> ids) {
		return new R<>(dictService.removeByIds(ids));
	}


	/**
	 * 所有类型字典
	 *
	 * @return 所有类型字典
	 */

	@GetMapping("/all")
	public R<String> getAll() {
		List<Dict> list = dictService.list();
		return new R<>(Json.toJsonString(list));
	}

}
