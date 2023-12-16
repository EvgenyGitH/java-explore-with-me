package ru.practicum.mainservice.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.category.dto.CategoryDto;
import ru.practicum.mainservice.category.dto.NewCategoryDto;
import ru.practicum.mainservice.category.mapper.CategoryMapper;
import ru.practicum.mainservice.category.model.Category;
import ru.practicum.mainservice.category.repository.CategoryRepository;
import ru.practicum.mainservice.event.repository.EventRepository;
import ru.practicum.mainservice.exceprion.DataConflictException;
import ru.practicum.mainservice.exceprion.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImp implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    // --- ADMIN ---
    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category categoryToSave = CategoryMapper.newToCategory(newCategoryDto);
        Category category;
        try {
            category = categoryRepository.save(categoryToSave);
        } catch (DataIntegrityViolationException exception) {
            throw new DataConflictException(exception.getMessage());
        }

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            throw new DataConflictException("The category is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        Category savedCategory = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Category with id=" + catId + " was not found"));
        savedCategory.setName(newCategoryDto.getName());
        Category category;
        try {
            category = categoryRepository.save(savedCategory);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DataConflictException(exception.getMessage());
        }
        return CategoryMapper.toCategoryDto(category);
    }

    // --- PUBLIC ---
    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoriesById(Long catId) {
        Category savedCategory = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Category with id=" + catId + " was not found"));
        return CategoryMapper.toCategoryDto(savedCategory);
    }

}
