package com.punish.Model;

import java.util.List;

public record PaginaResult<T>(List<T> data, long total, int page, int size) {}
