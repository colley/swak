package com.swak.autoconfigure.controller;

import com.google.common.collect.Lists;
import com.swak.core.monitor.model.*;
import com.swak.core.monitor.system.*;
import com.swak.core.monitor.tools.ByteToM;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * The type Monitor controller.
 * @author colley.ma
 * @since 2022/9/13 16:07
 */
@RestController
@RequestMapping("monitor")
public class MonitorController{

	/**
	 * 内存监控
	 *
	 * @return memory
	 * @throws Exception the exception
	 */
	@RequestMapping(path="memory",method=RequestMethod.GET)
	public Memory memory() throws Exception {
		return MemoryMonitor.getMemory();
	}

	/**
	 * 内存分代监控
	 *
	 * @return list
	 * @throws Exception the exception
	 */
	@RequestMapping(path="memoryPool",method=RequestMethod.GET)
	public List<MemoryPoolStr> memoryPool() throws Exception {
		List<MemoryPool> pools =  MemoryPoolMonitor.getPools();
		List<MemoryPoolStr> stars = Lists.newArrayList();
		for (MemoryPool mp : pools) {
			MemoryPoolStr str = new MemoryPoolStr();
			str.setCommitted(ByteToM.convert(mp.getCommitted()));
			str.setId(mp.getId());
			str.setInit(ByteToM.convert(mp.getInit()));
			str.setMax(ByteToM.convert(mp.getMax()));
			str.setName(mp.getName());
			str.setType(mp.getType());
			str.setUsed(ByteToM.convert(mp.getUsed()));
			stars.add(str);
		}
		return stars;
	}

	/**
	 * 内存分代监控
	 *
	 * @return list
	 * @throws Exception the exception
	 */
	@RequestMapping(path="threads",method=RequestMethod.GET)
	public List<SunThread> threads() throws Exception {
		return ThreadMonitor.geThreads();
	}

	/**
	 * 系统运行时数据监控
	 *
	 * @return runtime information
	 * @throws Exception the exception
	 */
	@RequestMapping(path="runtime",method=RequestMethod.GET)
	public RuntimeInformation runtime() throws Exception {
		return RuntimeMonitor.getRuntimeInformation();
	}

	/**
	 * Garbages list.
	 *
	 * @return the list
	 * @throws Exception the exception
	 */
	@RequestMapping(path="garbage",method=RequestMethod.GET)
	public List<Garbage> garbage() throws Exception{
		return GarbageMonitor.garbages();
	}

	/**
	 * Class loading class loading.
	 *
	 * @return the class loading
	 * @throws Exception the exception
	 */
	@RequestMapping(path="classloading",method=RequestMethod.GET)
	public ClassLoading classLoading() throws Exception{
		
		return ClassLoadingMonitor.classLoading();
	}

	/**
	 * Nio buffer pool list.
	 *
	 * @return the list
	 * @throws Exception the exception
	 */
	@RequestMapping(path="niobuffer",method=RequestMethod.GET)
	public List<NioBufferPool> nioBufferPool() throws Exception{
		return NioBufferPoolMonitor.bufferPools();
	}
}
