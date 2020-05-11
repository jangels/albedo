package com.albedo.java.modules.gen.service;

import com.albedo.java.common.core.vo.PageModel;
import com.albedo.java.common.persistence.service.DataService;
import com.albedo.java.modules.gen.domain.Scheme;
import com.albedo.java.modules.gen.domain.dto.SchemeDto;
import com.albedo.java.modules.gen.repository.SchemeRepository;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

public interface SchemeService extends DataService<SchemeRepository, Scheme, SchemeDto, String> {
	List<Scheme> findAll(String id);

	String generateCode(SchemeDto schemeDataVo);

	Map<String, Object> findFormData(SchemeDto schemeDataVo, String loginId);

	/**
	 * 分页查询用户信息（含有角色信息）
	 *
	 * @param pm 分页对象
	 * @return
	 */
	IPage getSchemeVoPage(PageModel pm);

	Map<String, Object> previewCode(String id, String username);
}
