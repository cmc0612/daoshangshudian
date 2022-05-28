package cn.itcast.itcaststore.web.servlet.manager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;//可以把表单域中的各种类型数据解析出来，并且实现多文件上传。
import org.apache.commons.io.IOUtils;//要导的包
import cn.itcast.itcaststore.domain.Product;
import cn.itcast.itcaststore.exception.AddProductException;
import cn.itcast.itcaststore.service.ProductService;
import cn.itcast.itcaststore.utils.FileUploadUtils;
import cn.itcast.itcaststore.utils.IdUtils;
/**
 * 后台系统中
 * 添加商品的servlet的类
 */
public class AddProductServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 创建空的商品对象
		Product p = new Product();
		Map<String, String> map = new HashMap<String, String>();
		// 把商品的id和生成的32位字符绑定
		map.put("id", IdUtils.getUUID());
         //在JSP页面中，form表单中method必须设置为post，并且要设置enctype=”multipart/form-data”：
		/*DiskFileItemFactory 是创建FileItem 对象的工厂，这个工厂类常用方法：	
	*/
		DiskFileItemFactory dfif = new DiskFileItemFactory();
		// 设置临时文件存储位置
	/*	setRepository(Java.io.File repository) ：指定临时文件目录，默认值为System.getProperty(“java.io.tmpdir”)*/
		dfif.setRepository(new File(this.getServletContext().getRealPath("/temp")));
		// 设置上传文件缓存大小为10m，什么都不写的话默认为10k
	/*	 setSizeThreshold(int sizeThreshold) ：设置内存缓冲区的大小，
		 默认值为10K。当上传文件大于缓冲区大小时，fileupload组件将使用临时文件缓存上传文件。*/
		dfif.setSizeThreshold(1024 * 1024 * 10);
		// 创建上传组件
		ServletFileUpload upload = new ServletFileUpload(dfif);
		// 处理上传文件中文乱码
		upload.setHeaderEncoding("utf-8");
		try {
			// 解析request得到所有的FileItem
			List<FileItem> items = upload.parseRequest(request);
			// 遍历所有FileItem
			for (FileItem item : items) {
				// 判断当前是否是上传组件
				if (item.isFormField()) {//判断是不是普通文本单字段方法，或者是文件字段，普通文本就是true
					// 不是上传组件
					String fieldName = item.getFieldName(); // 获取组件名称
					String value = item.getString("utf-8"); // 解决乱码问题
					map.put(fieldName, value);
				} else {
					// 是上传组件
					// 得到上传文件真实名称
					
					String fileName = item.getName();//如果是普通字段，那么结果就是null，否则就是字符串类型;
					                              // 不同浏览器获得的文件名是不一样的，有的是地址加文件名，有的只有文件名,例如"C:\文件名"，和文件名
					fileName = FileUploadUtils.subFileName(fileName);

					// 得到随机名称
					String randomName = FileUploadUtils
							.generateRandonFileName(fileName);

					// 得到随机目录
					String randomDir = FileUploadUtils
							.generateRandomDir(randomName);
					// 图片存储父目录
					String imgurl_parent = "/productImg" + randomDir;

					File parentDir = new File(this.getServletContext()
							.getRealPath(imgurl_parent));
					// 验证目录是否存在，如果不存在，创建出来
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					String imgurl = imgurl_parent + "/" + randomName;

					map.put("imgurl", imgurl);

					IOUtils.copy(item.getInputStream(), new FileOutputStream(
							new File(parentDir, randomName)));
					item.delete();
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		try {
			// 将数据封装到javaBean中
			BeanUtils.populate(p, map);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		ProductService service = new ProductService();
		try {
			// 调用service完成添加商品操作
			service.addProduct(p);
			response.sendRedirect(request.getContextPath()
					+ "/listProduct");
			return;
		} catch (AddProductException e) {
			e.printStackTrace();
			response.getWriter().write("添加商品失败");
			return;
		}
	}
}