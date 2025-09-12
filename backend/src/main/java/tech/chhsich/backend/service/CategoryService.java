package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.mapper.LtypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private LtypeMapper ltypeMapper;

    // 获取所有未删除的分类
    public List<Ltype> getAllCategories() {
        return ltypeMapper.findByCatelock(0); // 0表示未删除的分类
    }

    // 按名称查询分类
    public Ltype getCategoryByName(String catename) {
        return ltypeMapper.findByCatename(catename);
    }

    // 按ID查询分类，使用MyBatis的查询方式
    public Ltype getCategoryById(Long id) {
        return ltypeMapper.findById(id); // 使用Mapper中新增的findById方法
    }
}
