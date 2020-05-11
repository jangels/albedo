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

package com.albedo.java.modules.sys.service.impl;

import com.albedo.java.common.core.util.CollUtil;
import com.albedo.java.common.core.util.StringUtil;
import com.albedo.java.common.core.vo.TreeNode;
import com.albedo.java.common.data.util.QueryWrapperUtil;
import com.albedo.java.common.persistence.service.impl.TreeServiceImpl;
import com.albedo.java.modules.sys.domain.Dept;
import com.albedo.java.modules.sys.domain.DeptRelation;
import com.albedo.java.modules.sys.domain.dto.DeptDto;
import com.albedo.java.modules.sys.domain.dto.DeptQueryCriteria;
import com.albedo.java.modules.sys.repository.DeptRepository;
import com.albedo.java.modules.sys.service.DeptRelationService;
import com.albedo.java.modules.sys.service.DeptService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门管理 服务实现类
 * </p>
 *
 * @author somewhere
 * @since 2019/2/1
 */
@Service
@AllArgsConstructor
public class DeptServiceImpl extends
	TreeServiceImpl<DeptRepository, Dept, DeptDto> implements DeptService {
	private final DeptRelationService deptRelationService;

	/**
	 * 添加信息部门
	 *
	 * @param deptDto 部门
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdate(DeptDto deptDto) {
		boolean add = StringUtil.isEmpty(deptDto.getId());
		super.saveOrUpdate(deptDto);
		if (add) {
			deptRelationService.saveDeptRelation(deptDto);
		} else {
			//更新部门关系
			DeptRelation relation = new DeptRelation();
			relation.setAncestor(deptDto.getParentId());
			relation.setDescendant(deptDto.getId());
			deptRelationService.updateDeptRelation(relation);
		}
	}


	/**
	 * 删除部门
	 *
	 * @param ids 部门 ID
	 * @return 成功、失败
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeDeptByIds(Set<String> ids) {
		ids.forEach(id -> {
			//级联删除部门
			Set<String> idList = deptRelationService
				.list(Wrappers.<DeptRelation>query().lambda()
					.eq(DeptRelation::getAncestor, id))
				.stream()
				.map(DeptRelation::getDescendant)
				.collect(Collectors.toSet());

			if (CollUtil.isNotEmpty(idList)) {
				this.removeByIds(idList);
			}

			//删除部门级联关系
			deptRelationService.removeDeptRelationById(id);
		});

		return Boolean.TRUE;
	}


	@Override
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public List<String> findDescendantIdList(String deptId) {
		List<String> descendantIdList = deptRelationService
			.list(Wrappers.<DeptRelation>query().lambda()
				.eq(DeptRelation::getAncestor, deptId))
			.stream().map(DeptRelation::getDescendant)
			.collect(Collectors.toList());
		return descendantIdList;
	}

	/**
	 * 查询用户部门树
	 *
	 * @return
	 */
	@Override
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public Set<TreeNode> findDeptTrees(DeptQueryCriteria deptQueryCriteria, String deptId) {
		deptQueryCriteria.setDeptIds(findDescendantIdList(deptId));
		List<Dept> deptList = baseMapper.selectList(QueryWrapperUtil.getWrapper(deptQueryCriteria));
		return getNodeTree(deptList);
	}

}
