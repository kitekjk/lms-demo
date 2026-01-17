import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/admin/store/domain/models/store.dart';

class StoreApiService {
  final Dio dio;

  StoreApiService(this.dio);

  Future<List<Store>> getAllStores() async {
    try {
      final response = await dio.get(ApiEndpoints.stores);
      final data = response.data as Map<String, dynamic>;
      final stores = (data['stores'] as List)
          .map((e) => Store.fromJson(e as Map<String, dynamic>))
          .toList();
      return stores;
    } catch (e) {
      rethrow;
    }
  }

  Future<Store> getStore(String id) async {
    try {
      final response = await dio.get(ApiEndpoints.storeById(id));
      return Store.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<Store> createStore({
    required String name,
    required String location,
  }) async {
    try {
      final response = await dio.post(
        ApiEndpoints.stores,
        data: {
          'name': name,
          'location': location,
        },
      );
      return Store.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<Store> updateStore({
    required String id,
    required String name,
    required String location,
  }) async {
    try {
      final response = await dio.put(
        ApiEndpoints.storeById(id),
        data: {
          'name': name,
          'location': location,
        },
      );
      return Store.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<void> deleteStore(String id) async {
    try {
      await dio.delete(ApiEndpoints.storeById(id));
    } catch (e) {
      rethrow;
    }
  }
}
