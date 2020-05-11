/**
 * Copyright &copy; 2018 <a href="https://github.com/somewhereMrli/albedo-boot">albedo-boot</a> All rights reserved.
 */
package com.albedo.java.modules.sys.web;

import com.albedo.java.common.core.util.BeanUtil;
import com.albedo.java.common.core.util.R;
import com.albedo.java.common.core.vo.PageModel;
import com.albedo.java.common.log.annotation.Log;
import com.albedo.java.common.log.enums.BusinessType;
import com.albedo.java.common.util.ExcelUtil;
import com.albedo.java.modules.sys.domain.vo.LogLoginExcelVo;
import com.albedo.java.modules.sys.service.LogLoginService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录日志Controller 登录日志
 *
 * @author admin
 * @version 2019-08-15 09:32:16
 */
@RestController
@RequestMapping(value = "${application.admin-path}/sys/log-login")
@Slf4j
@AllArgsConstructor
public class LogLoginResource {

	private final LogLoginService logLoginService;

	/**
	 * GET / : get all logLogin.
	 *
	 * @param pm the pagination information
	 * @return the R with status 200 (OK) and with body all logLogin
	 */

	@PreAuthorize("@pms.hasPermission('sys_logLogin_view')")
	@GetMapping
	public R getPage(PageModel pm) {
		return R.buildOkData(logLoginService.page(pm));
	}


	/**
	 * DELETE //:ids : delete the "ids" LogLogin.
	 *
	 * @param ids the id of the logLogin to delete
	 * @return the R with status 200 (OK)
	 */
	@PreAuthorize("@pms.hasPermission('sys_logLogin_del')")
	@Log(value = "登录日志", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R delete(@RequestBody Set<String> ids) {
		log.debug("REST request to delete LogLogin: {}", ids);
		logLoginService.removeByIds(ids);
		return R.buildOk("删除登录日志成功");
	}

	@Log(value = "登录日志", businessType = BusinessType.EXPORT)
	@GetMapping(value = "/export")
	@PreAuthorize("@pms.hasPermission('sys_logOperate_export')")
	public R export(PageModel pm) {
		ExcelUtil<LogLoginExcelVo> util = new ExcelUtil(LogLoginExcelVo.class);
		return util.exportExcel(logLoginService.list(Wrappers.emptyWrapper()).stream()
			.map(logLogin -> BeanUtil.copyPropertiesByClass(logLogin, LogLoginExcelVo.class))
			.collect(Collectors.toList()), "登录日志");
	}
}
